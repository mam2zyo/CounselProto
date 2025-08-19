import React from 'react';

const Footer = ({ author }) => {
  const year = new Date().getFullYear();

  return (
    <footer style={{ marginTop: '40px', padding: '20px', background: '#eee', textAlign: 'center' }}>
      <small>© {year} {author} | React 구조적 컴포넌트 실습</small>
    </footer>
  );
};

export default Footer;
