import React from 'react';
import getMuiTheme        from 'material-ui/styles/getMuiTheme';
import MuiThemeProvider   from 'material-ui/styles/MuiThemeProvider';
import {List, ListItem} from 'material-ui/List';
import ContentInbox from 'material-ui/svg-icons/content/inbox';
import ActionGrade from 'material-ui/svg-icons/action/grade';
import ContentSend from 'material-ui/svg-icons/content/send';
import ContentDrafts from 'material-ui/svg-icons/content/drafts';
import Divider from 'material-ui/Divider';
import ActionInfo from 'material-ui/svg-icons/action/info';

class MaterialUINodesList extends React.Component {
  constructor(props) {
    super(props);
  }
  static propTypes = {
    items: React.PropTypes.array
  };

  static defaultProps = {
    items: []
  };

  render(){  
    return <List>

    {
    this.props.items.map((item,idx)=>(

       <ListItem key={idx}>{item.text}</ListItem>
      ))}
    </List> 

  }
    

}



export default MaterialUINodesList;