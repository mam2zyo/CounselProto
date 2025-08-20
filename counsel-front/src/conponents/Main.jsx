import React from 'react';
import UserInput from './UserInput';
import EmailInput from './EmailInput';
import UserInfo from './UserInfo';
import Counter from './Counter';
import ThemeToggle from './ThemeToggle';

const Main = ({
  username,
  setUsername,
  age,
  setAge,
  email,
  setEmail,
  count,
  setCount,
  darkMode,
  setDarkMode
}) => {
  return (
    <main style={{ padding: '20px' }}>
      <UserInput username={username} setUsername={setUsername} age={age} setAge={setAge} />
      <EmailInput email={email} setEmail={setEmail} />
      <UserInfo username={username} age={age} email={email} />
      <Counter count={count} setCount={setCount} />
      <ThemeToggle darkMode={darkMode} setDarkMode={setDarkMode} />
    </main>
  );
};

export default Main;
