import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/runtimes`;

/**
 * List all exiting runtimes.
 *
 * @param {string} token the access token
 * @param {function} setRuntimes the function to set the retrieved runtimes
 * @param {function} setError the function to set the error if one occurred
 */
export async function listRuntimes(token, setRuntimes, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setRuntimes(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get the function template for a runtime.
 *
 * @param {number} id the id of the runtime
 * @param {string} token the access token
 * @param {function} setTemplate the function to set the retrieved template
 * @param {function} setError the function to set the error if one occurred
 */
export async function getRuntimeTemplate(id, token, setTemplate, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}/template`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setTemplate(() => data.template);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
