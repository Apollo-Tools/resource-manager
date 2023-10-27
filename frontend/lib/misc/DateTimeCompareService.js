/**
 * Compute the time difference between two unix epoch timestamps.
 *
 * @param {number} start the stat timestamp
 * @param {number} end the end timestamp
 * @return {string} the time difference formatted as mm:ss.000
 */
export function computeTimeDifference(start, end) {
  const startDate = new Date(start);
  const endDate = new Date(end);

  const timeDifference = endDate - startDate;

  const totalMilliseconds = Math.abs(timeDifference);
  const totalSeconds = Math.floor(totalMilliseconds / 1000);
  const minutes = Math.floor(totalSeconds / 60).toString().padStart(2, '0');
  const seconds = (totalSeconds % 60).toString().padStart(2, '0');
  const milliseconds = (totalMilliseconds % 1000).toString().padStart(3, '0');

  return `${minutes}:${seconds}.${milliseconds}`;
}
