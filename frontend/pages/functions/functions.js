import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Divider, Segmented, Switch, Typography} from 'antd';
import FunctionTable from '../../components/functions/FunctionTable';
import NewEntityButton from '../../components/misc/NewEntityButton';
import {useState} from 'react';
import ArtifactTypeTable from '../../components/artifacttypes/ArtifactTypeTable';

const Functions = () => {
  const [selectedSegment, setSelectedSegment] = useState('Functions');
  const segments = ['Functions', 'Types'];
  const [showPublicFunctions, setShowPublicFunctions] = useState(false);

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
              <div className="grid grid-cols-6 gap-5 content-center">
                <div className="grid col-span-1 content-center">
                    <NewEntityButton name="Function" marginBottom={0}/>
                </div>
                <div className="grid col-start-6 col-span-1 content-center justify-end">
                    <Switch
                        checkedChildren="public functions"
                        unCheckedChildren="own function"
                        checked={showPublicFunctions}
                        onChange={setShowPublicFunctions}
                    />
                </div>
                <div className="col-span-full">
                    <FunctionTable publicFunctions={showPublicFunctions}/>
                </div>
              </div> :
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
