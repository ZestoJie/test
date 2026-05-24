import React from "react";

const DashboardLayout: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  return <main>{children}</main>;
};

export default DashboardLayout;
