import Head from 'next/head';
import {siteTitle} from './Sidebar';
import {Button, Result, Typography} from 'antd';
import {FunctionOutlined, SmileOutlined, UndoOutlined} from '@ant-design/icons';
import Link from 'next/link';
import PropTypes from 'prop-types';


const NewEntityContainer = ({entityName, newEntity, setNewEntity, rootPath, children}) => {
  const onClickRestart = () => {
    setNewEntity(null);
  };

  return <>
    <Head>
      <title>{`${siteTitle}: New ${entityName}`}</title>
    </Head>
    <div className="card container w-full md:w-11/12 max-w-7xl p-10 mt-2 mb-2">
      <Typography.Title level={2}>New {entityName}</Typography.Title>
      {newEntity ?
        <Result
          icon={<SmileOutlined />}
          title="The function type has been created!"
          extra={
            <>
              <Button type="primary" icon={<UndoOutlined />} onClick={onClickRestart}>Restart</Button>
              <Link href={rootPath}>
                <Button type="default" icon={<FunctionOutlined />}>All Functions</Button>
              </Link>
            </>
          }
        /> :
          <>{children}</>}
    </div>
  </>;
};

NewEntityContainer.propTypes = {
  entityName: PropTypes.string.isRequired,
  newEntity: PropTypes.object.isRequired,
  setNewEntity: PropTypes.func.isRequired,
  rootPath: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
};

export default NewEntityContainer;
