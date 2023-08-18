import Head from 'next/head';
import {siteTitle} from './Sidebar';
import {Button, Result, Typography} from 'antd';
import {FunctionOutlined, SmileOutlined, UndoOutlined} from '@ant-design/icons';
import Link from 'next/link';
import PropTypes from 'prop-types';


const NewEntityContainer = ({entityName, isFinished, onReset, rootPath, children, overviewName}) => {
  const backButtonLabel = overviewName == null ? entityName : overviewName;

  const onClickRestart = () => {
    onReset();
  };

  return <>
    <Head>
      <title>{`${siteTitle}: New ${entityName}`}</title>
    </Head>
    <div className="default-card">
      <Typography.Title level={2}>New {entityName}</Typography.Title>
      {isFinished ?
        <Result
          icon={<SmileOutlined />}
          title={`The ${entityName} has been created!`}
          extra={
            <>
              <Button type="primary" icon={<UndoOutlined />} onClick={onClickRestart}>Restart</Button>
              <Link href={rootPath}>
                <Button type="default" icon={<FunctionOutlined />}>All {backButtonLabel}s</Button>
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
  isFinished: PropTypes.bool.isRequired,
  onReset: PropTypes.func.isRequired,
  rootPath: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
  overviewName: PropTypes.string,
};

export default NewEntityContainer;
