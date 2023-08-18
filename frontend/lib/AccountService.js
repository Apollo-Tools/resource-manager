import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/accounts`;

/**
 * Login with username and password and set the retrieved access token.
 *
 * @param {string} username the username
 * @param {string} password the password
 * @param {function} setToken the function to set the access token
 * @param {function} setError the function to set the error if one occurs
 */
export async function getLogin(username, password, setToken, setError) {
  try {
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
    const data = await response.json();
    setToken(data.token);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Update the password of the user.
 *
 * @param {string} oldPassword the old password
 * @param {string} newPassword the new password
 * @param {string} token the access token
 * @param {function} setResponse the function to set the response
 * @param {function} setError the function to set the error if one occurs
 */
export async function changePassword(oldPassword, newPassword, token, setResponse, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
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
    setResponse(response);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get details of the currently logged in account.
 *
 * @param {string} token the access token
 * @param {function} setAccount the function to set the account details
 * @param {function} setError the function to set the error if one occurs
 */
export async function getMyAccount(token, setAccount, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/me`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setAccount(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Sign up a new user account.
 *
 * @param {string} username the username
 * @param {string} password the password
 * @param {function} setResponse the function to set the response
 * @param {function} setError the function to set the error if one occurs
 */
export async function signUp(username, password, setResponse, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/signup`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username,
        password: password,
      }),
    });
    setResponse(response);
  } catch (error) {
    setError(error);
    console.log(error);
  }
}

/**
 * List all accounts.
 *
 * @param {string} token the access token
 * @param {function} setAccounts the function to set the retrieved accounts
 * @param {function} setError the function to set the error if one occurred
 */
export async function listAccounts(token, setAccounts, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setAccounts(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
