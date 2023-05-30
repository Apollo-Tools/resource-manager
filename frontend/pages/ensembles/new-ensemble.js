import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {useState} from 'react';
import {Result, Button, Typography} from 'antd';
import {DatabaseOutlined, SmileOutlined, UndoOutlined} from '@ant-design/icons';
import NewEnsembleForm from '../../components/ensembles/NewEnsembleForm';
import Link from 'next/link';


const NewEnsemble = () => {
  const [newEnsemble, setNewEnsemble] = useState(null);

  const onClickRestart = () => {
    setNewEnsemble(null);
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
            extra={<>
              <Button type="primary" icon={<UndoOutlined />} onClick={onClickRestart}>Restart</Button>
              <Link href={`/ensembles/ensembles`}>
                <Button type="default" icon={<DatabaseOutlined />}>All Ensembles</Button>
              </Link>
            </>}
          />:
          <NewEnsembleForm setNewEnsemble={setNewEnsemble}/>
        }
      </div>
    </>
  );
};

export default NewEnsemble;
