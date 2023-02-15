import { useEffect, useRef } from 'react';

/** source: https://blog.bitsrc.io/polling-in-react-using-the-useinterval-custom-hook-e2bcefda4197 **/
export function useInterval(callback, delay) {
  const savedCallback = useRef();
  // Remember the latest callback.
  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);
  // Set up the interval.
  useEffect(() => {
    function tick() {
      savedCallback.current();
    }
    if (delay !== null) {
      let id = setInterval(tick, delay);
      return () => clearInterval(id);
    }
  }, [callback, delay]);
}