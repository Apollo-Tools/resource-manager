import {useEffect, useRef} from 'react';

/* source: https://blog.bitsrc.io/polling-in-react-using-the-useinterval-custom-hook-e2bcefda4197 */
/**
 * A custom react hook that can be used to implement polling.
 *
 * @param {function} callback the callback function
 * @param {number} delay the delay in milliseconds
 */
export function useInterval(callback, delay) {
  const savedCallback = useRef();
  // Remember the latest callback.
  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);
  // Set up the interval.
  useEffect(() => {
    const tick = () => {
      savedCallback.current();
    };
    if (delay !== null) {
      const id = setInterval(tick, delay);
      return () => clearInterval(id);
    }
  }, [callback, delay]);
}
