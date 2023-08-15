import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Divider, Segmented, Typography} from 'antd';
import FunctionTable from '../../components/functions/FunctionTable';
import NewEntityButton from '../../components/misc/NewEntityButton';
import {useState} from 'react';
import ArtifactTypeTable from '../../components/artifacttypes/ArtifactTypeTable';

const Functions = () => {
  const [selectedSegment, setSelectedSegment] = useState('Functions');
  const segments = ['Functions', 'Types'];
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Functions`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>All Functions</Typography.Title>
        <Segmented options={segments} value={selectedSegment}
          onChange={(e) => setSelectedSegment(e)} size="large" block/>
        <Divider />
        {selectedSegment === 'Functions' ?
              <>
                <NewEntityButton name="Function" />
                <FunctionTable />
              </> :
              <>
                <NewEntityButton name="Function Type" path={`/functions/new-type`}/>
                <ArtifactTypeTable artifact="function" />
              </>
        }
      </div>
    </>
  );
};

export default Functions;
