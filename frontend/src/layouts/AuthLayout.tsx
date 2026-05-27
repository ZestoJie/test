import React from "react";

const AuthLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <main>{children}</main>;
};

export default AuthLayout;
