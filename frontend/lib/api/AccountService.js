import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/accounts`;

/**
 * Login with username and password and set the retrieved access token.
 *
 * @param {string} username the username
 * @param {string} password the password
 * @param {function} setToken the function to set the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 */
export async function getLogin(username, password, setToken, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username,
        password: password,
      }),
    });
    await setResult(response, setToken);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Update the password of the user.
 *
 * @param {string} oldPassword the old password
 * @param {string} newPassword the new password
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the response was valid
 */
export async function changePassword(oldPassword, newPassword, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/me`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        old_password: oldPassword,
        new_password: newPassword,
      }),
    });
    return await checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get details of the currently logged in account.
 *
 * @param {accountId} accountId the id of the account
 * @param {string} token the access token
 * @param {function} setAccount the function to set the account details
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 */
export async function getAccount(accountId, token, setAccount, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${accountId}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setAccount);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get details of the currently logged in account.
 *
 * @param {string} token the access token
 * @param {function} setAccount the function to set the account details
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 */
export async function getMyAccount(token, setAccount, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/me`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setAccount);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Sign up a new user account.
 *
 * @param {string} username the username
 * @param {string} password the password
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful else false
 */
export async function signUp(username, password, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/signup`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username,
        password: password,
      }),
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all accounts.
 *
 * @param {string} token the access token
 * @param {function} setAccounts the function to set the retrieved accounts
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listAccounts(token, setAccounts, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setAccounts);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Lock of an existing user.
 *
 * @param {number} accountId the id of the account
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful else false
 */
export async function lockUser(accountId, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${accountId}/lock`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Unlock an existing user.
 *
 * @param {number} accountId the id of the account
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful else false
 */
export async function unlockUser(accountId, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${accountId}/unlock`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}
