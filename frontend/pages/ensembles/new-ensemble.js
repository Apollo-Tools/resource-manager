import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {useEffect, useState} from 'react';
import {Result, Button, Typography} from 'antd';
import {SmileOutlined} from '@ant-design/icons';
import NewEnsembleForm from '../../components/ensembles/NewEnsembleForm';


const NewEnsemble = () => {
  const [newEnsemble, setNewEnsemble] = useState(null);

  useEffect(() => {
    if (newEnsemble != null) {
      console.log('new ensemble ' + newEnsemble);
    }
  }, [newEnsemble]);

  const onClickRestart = () => {
    setNewEnsemble(null);
    setFinished(false);
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: New Ensemble`}</title>
      </Head>
      <div className="card container w-full md:w-11/12 max-w-7xl p-10 mt-2 mb-2">
        <Typography.Title level={2}>New Ensemble</Typography.Title>
        {newEnsemble != null ?
          <Result
            icon={<SmileOutlined />}
            title="The ensemble has been created!"
            extra={<Button type="primary" onClick={onClickRestart}>Restart</Button>}
          />:
          <NewEnsembleForm setNewEnsemble={setNewEnsemble}/>
        }
      </div>
    </>
  );
};

export default NewEnsemble;
