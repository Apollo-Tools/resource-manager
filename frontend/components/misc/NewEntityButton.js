import Link from 'next/link';
import {Button} from 'antd';
import {PlusOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';


const NewEntityButton = ({name}) => {
  const lowerCaseName = name.toLowerCase();
  return (
    <Link href={`/${lowerCaseName}s/new-${lowerCaseName}`} className="block mb-5 w-fit">
      <Button type="default" icon={<PlusOutlined />}>New {name}</Button>
    </Link>
  );
};

NewEntityButton.propTypes = {
  name: PropTypes.string,
};

export default NewEntityButton;
