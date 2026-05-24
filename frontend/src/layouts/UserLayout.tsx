import React from "react";

const UserLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <main>{children}</main>;
};

export default UserLayout;
