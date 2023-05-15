import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/services`;

export async function createService(name, token, setFunction, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
      }),
    });
    const data = await response.json();
    setFunction(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function listServices(token, setServices, setError) {
  try {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setServices(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function getService(id, token, setService, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setService(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function deleteService(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
