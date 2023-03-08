import {useEffect, useState} from 'react';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Button, message, Result, Space, Typography} from 'antd';
import {SmileOutlined} from '@ant-design/icons';
import Link from 'next/link';
import NewReservationForm from '../../components/reservations/NewReservationForm';

const NewReservation = () => {
  const [error, setError] = useState(false);
  const [newReservation, setNewReservation] = useState();
  const [messageApi, contextHolder] = message.useMessage();

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

  const onClickRestart = () => {
    setNewReservation(null);
  };

  return (
    <>
      {contextHolder}
      <Head>
        <title>{`${siteTitle}: Resources`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>New Reservation</Typography.Title>
        {newReservation ?
          <Result
            icon={<SmileOutlined />}
            title="The reservation has been created!"
            extra={(<Space size={100}>
              <Link href={ `/reservations/${ newReservation.reservation_id }` }>
                <Button type="primary">Show</Button>
              </Link>
              <Button type="default" onClick={onClickRestart}>Restart</Button>
            </Space>
            )}
          /> :
          <>
            <NewReservationForm setNewReservation={setNewReservation} />
          </>
        }
      </div>
    </>
  );
};

export default NewReservation;
