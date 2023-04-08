import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import {Typography} from 'antd';
import ReservationTable from '../../components/reservations/ReservationTable';

const Reservations = () => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Reservations`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>My Reservations</Typography.Title>
        <ReservationTable />
      </div>
    </>
  );
};

export default Reservations;
