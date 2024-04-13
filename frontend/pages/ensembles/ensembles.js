import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Typography} from 'antd';
import NewEntityButton from '../../components/misc/NewEntityButton';
import EnsembleTable from '../../components/ensembles/EnsembleTable';
import PropTypes from 'prop-types';

const Ensembles = ({setError}) => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Ensembles`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>All Ensembles</Typography.Title>
        <NewEntityButton name="Ensemble"/>
        <EnsembleTable setError={setError}/>
      </div>
    </>
  );
};

Ensembles.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default Ensembles;
