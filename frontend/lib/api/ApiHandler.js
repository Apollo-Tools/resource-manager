/**
 * Handle an API call.
 *
 * @param {function} callback the api callback function
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 */
export async function handleApiCall(callback, setLoading, setError) {
  setLoading(true);
  try {
    return await callback();
  } catch (error) {
    console.error(error);
    setError(error);
  } finally {
    setLoading(false);
  }
}

/**
 * Check if a response is valid and throw an Error if it is not.
 *
 * @param {Response} response the response
 */
export async function checkResponseOk(response) {
  if (!response.ok) {
    throw new Error(await response.text());
  }
}

/**
 * Set the json result of a response.
 *
 * @param {Response} response the response
 * @param {function} setResult the function to set the result
 */
export async function setResult(response, setResult) {
  await checkResponseOk(response);
  const data = await response.json();
  setResult(data);
}
