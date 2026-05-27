import React from "react";

const Modal: React.FC<{ open: boolean; children: React.ReactNode }> = ({
  open,
  children,
}) => {
  return open ? <div>{children}</div> : null;
};

export default Modal;
