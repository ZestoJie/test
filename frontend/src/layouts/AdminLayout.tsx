import React from "react";

const AdminLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <main>{children}</main>;
};

export default AdminLayout;
