import React from 'react';

const Header = ({ title, subtitle }) => {
  return (
    <header style={{ background: '#333', color: '#fff', padding: '20px' }}>
      <h1>{title}</h1>
      <p>{subtitle}</p>
    </header>
  );
};

export default Header;
