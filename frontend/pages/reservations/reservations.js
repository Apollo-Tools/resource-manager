import {siteTitle} from '../../components/Sidebar';
import Head from 'next/head';
import {Button, Modal, Table, Typography} from 'antd';
import DateFormatter from '../../components/DateFormatter';
import {
  DisconnectOutlined,
  ExclamationCircleFilled,
  CheckCircleTwoTone,
  CloseCircleTwoTone,
} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {cancelReservation, listMyReservations} from '../../lib/ReservationService';

const {Column} = Table;
const {confirm} = Modal;

const Reservations = () => {
  const {token, checkTokenExpired} = useAuth();
  const [reservations, setReservations] = useState([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listMyReservations(token, setReservations, setError);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onClickDelete = async (id) => {
    if (!checkTokenExpired()) {
      await cancelReservation(id, token, setError)
          .then(() => {
            setReservations((prevReservation) =>
              prevReservation.map((reservation) => {
                if (reservation.reservation_id === id) {
                  reservation.is_active = false;
                }
                return reservation;
              }),
            );
          });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to cancel this reservation?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickDelete(id);
      },
    });
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: Reservations`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>My Reservations</Typography.Title>
        <Table dataSource={reservations} rowKey={(record) => record.reservation_id}>
          <Column title="Id" dataIndex="reservation_id" key="id"
            sorter={(a, b) => a.reservation_id - b.reservation_id}
          />
          <Column title="Is Active" dataIndex="is_active" key="is_active"
            render={(isActive) => isActive ? <CheckCircleTwoTone twoToneColor="#00ff00" className="text-lg"/> :
              <CloseCircleTwoTone twoToneColor="#ff0000" className="text-lg"/>}
            sorter={(a, b) => {
              if (a.is_active && b.is_active) return 0;
              else if (a.is_active) return -1;
              else if (b.is_active) return 1;
            }
            }
            defaultSortOrder="ascend"
          />
          <Column title="Created at" dataIndex="created_at" key="created_at"
            render={(createdAt) => <DateFormatter dateString={createdAt}/>}
            sorter={(a, b) => a.created_at - b.created_at}
          />
          <Column title="Action at" key="action"
            render={(_, record) => (
              <Button onClick={() => showDeleteConfirm(record.reservation_id)} icon={<DisconnectOutlined />}/>
            )}
          />
        </Table>
      </div>
    </>
  );
};

export default Reservations;
