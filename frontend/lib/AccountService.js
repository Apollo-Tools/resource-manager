import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/accounts`;
export async function login(username, password, setToken, setError) {
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

export async function getAccount(token, setAccount, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
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
