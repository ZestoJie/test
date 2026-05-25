import React from "react";

const PublicLayout: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  return <main>{children}</main>;
};

export default PublicLayout;
