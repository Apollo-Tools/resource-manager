import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/functions`;

export async function createFunction(runtimeId, name, code, token, setFunction, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
        runtime: {
          runtime_id: runtimeId,
        },
        code: code,
      }),
    });
    const data = await response.json();
    setFunction(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function listFunctions(token, setFunctions, setError) {
  try {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setFunctions(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function getFunction(id, token, setFunction, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setFunction(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function updateFunction(id, code, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        code: code,
      }),
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function deleteFunction(id, token, setError) {
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
