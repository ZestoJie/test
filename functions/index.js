const functions = require("firebase-functions");
const admin = require("firebase-admin");
const express = require("express");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

admin.initializeApp();
const db = admin.database();

const app = express();
app.use(express.json());

const allowedOrigin = process.env.FRONTEND_ORIGIN;
app.use(cors({ origin: allowedOrigin }));

function keyForEmail(email) {
  return encodeURIComponent(String(email).toLowerCase());
}

app.post("/api/auth/register", async (req, res) => {
  try {
    const { email, password, name, role } = req.body;
    if (!email || !password)
      return res.status(400).json({ error: "email and password required" });

    const key = `auth/users/${keyForEmail(email)}`;
    const snap = await db.ref(key).get();
    if (snap.exists())
      return res.status(400).json({ error: "User already exists" });

    const hash = await bcrypt.hash(password, 10);
    const id = Date.now();
    const payload = {
      email,
      passwordHash: hash,
      name: name || "",
      role: role || "User",
      id,
    };
    await db.ref(key).set(payload);
    return res.json({
      user: { email, name: payload.name, role: payload.role, id },
    });
  } catch (e) {
    console.error(e);
    return res.status(500).json({ error: e.message });
  }
});

app.post("/api/auth/login", async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password)
      return res.status(400).json({ error: "email and password required" });

    const key = `auth/users/${keyForEmail(email)}`;
    const snap = await db.ref(key).get();
    if (!snap.exists())
      return res.status(401).json({ error: "Invalid credentials" });

    const data = snap.val();
    const ok = await bcrypt.compare(password, data.passwordHash || "");
    if (!ok) return res.status(401).json({ error: "Invalid credentials" });

    const secret =
      functions.config().jwt && functions.config().jwt.secret
        ? functions.config().jwt.secret
        : process.env.JWT_SECRET || "dev-secret";
    const token = jwt.sign(
      { email: data.email, name: data.name, role: data.role, id: data.id },
      secret,
      { expiresIn: "24h" },
    );

    return res.json({
      token,
      user: {
        email: data.email,
        name: data.name,
        role: data.role,
        id: data.id,
      },
    });
  } catch (e) {
    console.error(e);
    return res.status(500).json({ error: e.message });
  }
});

exports.api = functions.https.onRequest(app);
