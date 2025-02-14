import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const useBlockPop = (pathname, options = { replace: true }) => {
  const navigate = useNavigate();

  useEffect(() => {
    const handlePopState = (event) => {
      console.log(event);
      navigate(pathname, options);
    };

    window.addEventListener('popstate', handlePopState);
    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, [navigate, pathname, options]);
};

export default useBlockPop;
