import PropTypes from 'prop-types';
import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';
import ProviderIcon from '../misc/ProviderIcon';


const ResourceDetailsCard = ({resource}) => {
  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      <TextDataDisplay label="Environment" value={resource.region.resource_provider.environment.environment} className="col-span-6"/>
      <TextDataDisplay label="Resource Type" value={resource.platform.resource_type.resource_type} className="col-span-6"/>
      <TextDataDisplay label="Platform" value={resource.platform.platform} className="col-span-6"/>
      <TextDataDisplay label="Provider"
        value={<>
          <ProviderIcon provider={resource.region.resource_provider.provider} className="mr-1"/>
          {resource.region.resource_provider.provider}
        </>}
        className="col-span-6" />
      <TextDataDisplay label="Region" value={resource.region.name} className="col-span-6" />
      <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={resource.created_at} />} className="col-span-6"/>
    </div>
  );
};

ResourceDetailsCard.propTypes = {
  resource: PropTypes.object.isRequired,
};

export default ResourceDetailsCard;
