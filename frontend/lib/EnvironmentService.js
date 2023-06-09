import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/environments`;

/**
 * List all exiting environments.
 *
 * @param {string} token the access token
 * @param {function} setEnvironments the function to set the retrieved environments
 * @param {function} setError the function to set the error if one occurred
 */
export async function listEnvironments(token, setEnvironments, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setEnvironments(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
