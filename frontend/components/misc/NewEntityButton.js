import Link from 'next/link';
import {Button} from 'antd';
import {PlusOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';


const NewEntityButton = ({name, path, marginBottom = true}) => {
  const lowerCaseName = name.toLowerCase();
  const href = path == null ? `/${lowerCaseName}s/new-${lowerCaseName}` : path;
  const styling = marginBottom ? 'block mb-5 w-fit' : 'block mb-0 w-fit';
  return (
    <Link href={href} className={styling}>
      <Button type="default" icon={<PlusOutlined />}>New {name}</Button>
    </Link>
  );
};

NewEntityButton.propTypes = {
  name: PropTypes.string.isRequired,
  path: PropTypes.string,
  marginBottom: PropTypes.bool,
};

export default NewEntityButton;
