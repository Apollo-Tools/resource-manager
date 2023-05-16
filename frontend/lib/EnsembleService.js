import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/ensembles`;

export async function createEnsemble(name, slos, resources, token, setEnsemble, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
        slos: slos,
        resources: resources,
      }),
    });
    const data = await response.json();
    setEnsemble(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function listEnsembles(token, setEnsembles, setError) {
  try {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setEnsembles(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function getEnsemble(id, token, setEnsemble, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setEnsemble(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function deleteEnsemble(id, token, setError) {
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

export async function validateEnsemble(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}/validate`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    console.log(data);
    return data.map((result) => result.is_valid).reduce((prev, current) => prev && current);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
