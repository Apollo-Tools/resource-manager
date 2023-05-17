import {Button, Input, Space} from 'antd';
import {SearchOutlined} from '@ant-design/icons';
import {useRef} from 'react';


const ColumnFilterDropdown = ({setSelectedKeys, selectedKeys, confirm, clearFilters, columnName}) => {
  const searchInput = useRef(null);
  const handleSearch = (selectedKeys, confirm) => {
    confirm();
  };
  const handleReset = (clearFilters) => {
    clearFilters();
  };

  return (
    <div
      style={{
        padding: 8,
      }}
      onKeyDown={(e) => e.stopPropagation()}
    >
      <Input
        ref={searchInput}
        placeholder={`Search ${columnName}`}
        value={selectedKeys[0]}
        onChange={(e) => setSelectedKeys(e.target.value ? [e.target.value] : [])}
        onPressEnter={() => handleSearch(selectedKeys, confirm)}
        style={{
          marginBottom: 8,
          display: 'block',
        }}
      />
      <Space>
        <Button
          type="primary"
          onClick={() => handleSearch(selectedKeys, confirm)}
          icon={<SearchOutlined />}
          size="small"
          style={{
            width: 90,
          }}
        >
          Search
        </Button>
        <Button
          onClick={() => clearFilters && handleReset(clearFilters)}
          size="small"
          style={{
            width: 90,
          }}
        >
          Reset
        </Button>
      </Space>
    </div>
  );
};

export default ColumnFilterDropdown;
