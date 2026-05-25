const BASE_URL = "https://your-railway-domain.up.railway.app";

export async function getUsers() {
  const res = await fetch(`${BASE_URL}/api/users`);
  return res.json();
}

export async function createUser(user: any) {
  const res = await fetch(`${BASE_URL}/api/users`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(user),
  });

  return res.json();
}