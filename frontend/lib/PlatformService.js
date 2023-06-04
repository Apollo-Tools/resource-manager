import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/platforms`;

/**
 * List all platforms.
 *
 * @param {string} token the access token
 * @param {function} setPlatforms the function to set the retrieved platforms
 * @param {function} setError the function to set the error if one occurred
 */
export async function listPlatforms(token, setPlatforms, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setPlatforms(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
