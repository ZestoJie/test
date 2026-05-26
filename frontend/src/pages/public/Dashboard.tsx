import { useNavigate } from "react-router-dom";

export default function Dashboard() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: 20 }}>
      <h1>Dashboard</h1>
      <div>
        <button onClick={() => navigate("/")}>Index</button>
      </div>
    </div>
  );
}
