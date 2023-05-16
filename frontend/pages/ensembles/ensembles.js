import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Typography} from 'antd';
import NewEntityButton from '../../components/misc/NewEntityButton';
import EnsembleTable from '../../components/ensembles/EnsembleTable';

const Ensembles = () => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Ensembles`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>All Ensembles</Typography.Title>
        <NewEntityButton name="Ensemble"/>
        <EnsembleTable />
      </div>
    </>
  );
};

export default Ensembles;
