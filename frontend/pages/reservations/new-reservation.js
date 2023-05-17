import {useEffect, useState} from 'react';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {message, Steps, Typography} from 'antd';
import NewReservationEnsemble from '../../components/reservations/NewReservationEnsemble';
import NewResourceReservations from '../../components/reservations/NewResourceReservations';
import AddCredentials from '../../components/reservations/AddCredentials';

const steps = [
  {
    title: 'Select ensemble',
  },
  {
    title: 'Select resources',
  },
  {
    title: 'Add credentials',
  },
  {
    title: 'Finished',
  },
];

const NewReservation = () => {
  const [error, setError] = useState(false);
  const [newReservation, setNewReservation] = useState();
  const [selectedEnsembleId, setSelectedEnsembleId] = useState();
  const [messageApi, contextHolder] = message.useMessage();
  const [current, setCurrent] = useState(0);
  const [functionResources, setFunctionResources] = useState(new Map());
  const [serviceResources, setServiceResources] = useState(new Map());

  const items = steps.map((item) => ({
    key: item.title,
    title: item.title,
  }));

  const next = () => {
    setCurrent(current + 1);
  };
  const prev = () => {
    setCurrent(current - 1);
  };


  useEffect(() => {
    if (newReservation != null) {
      messageApi.open({
        type: 'success',
        content: `Reservation with id '${newReservation.reservation_id}' has been created!`,
      });
    }
  }, [newReservation]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  return (
    <>
      {contextHolder}
      <Head>
        <title>{`${siteTitle}: Resources`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10 ">
        <Typography.Title level={2}>New Reservation</Typography.Title>
        <Steps current={current} items={items} className="mb-1 p-5 shadow-lg bg-cyan-50"/>
        {current === 0 &&
          <NewReservationEnsemble value={selectedEnsembleId} next={next} setSelectedEnsemble={setSelectedEnsembleId} />}
        {current === 1 &&
          <NewResourceReservations
            functionResources={functionResources}
            serviceResources={serviceResources}
            ensembleId={selectedEnsembleId}
            setFunctionResources={setFunctionResources}
            setServiceResources={setServiceResources}
            next={next}
            prev={prev}
          />
        }
        {current === 2 &&
          <AddCredentials
            serviceResources={serviceResources}
            functionResources={functionResources}
            next={next}
            prev={prev}
          />
        }
      </div>
    </>
  );
};

export default NewReservation;
