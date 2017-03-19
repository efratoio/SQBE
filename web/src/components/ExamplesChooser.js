import React, {Component} from 'react';
import AutoComplete from 'material-ui/AutoComplete';
import getMuiTheme        from 'material-ui/styles/getMuiTheme';
import MuiThemeProvider   from 'material-ui/styles/MuiThemeProvider';
import JSONP              from 'jsonp';
import  MaterialUINodesList from './ExamplesPresenter';
import IconButton from 'material-ui/IconButton';
import {red500, yellow500, blue500, grey500} from 'material-ui/styles/colors';
import ActionSend from 'material-ui/svg-icons/content/send';
import conf from "./constants"
const ajaxCallURL = `http://localhost:8080/SandboxServlet?prefix=`;



class MaterialUIAutocomplete extends Component {
  static propTypes = {
    switchTab: React.PropTypes.func.isRequired,
  };

  
  constructor(props) {
    super(props);
    this.onNewRequest = this.onNewRequest.bind(this);
    this.state = conf.debug?{dataSource: [{text:"a",value:1},
        {text:"b",value:2},{text:"c",value:3},{text:"d",value:4},{text:"e",vaue:5}] ,
        inputValue : '',
      chosenNodes : [{text:"b",value:2}],
      disable : true,
      color : blue500}: {
      dataSource : [],
      inputValue : '',
      chosenNodes : [],
      disable : false,
      color : grey500
    };
    
    
    if(!conf.debug){
    const xhr = new XMLHttpRequest();

    URL = ajaxCallURL+"";
    xhr.open('GET', URL);
    xhr.addEventListener('load', () => {
      const results = JSON.parse(xhr.responseText);    
      this.setState({dataSource: results});
    });
    xhr.send();
   }

  }


  onNewRequest(chosenRequest) {
    const lst =this.state.chosenNodes;
     this.setState({chosenNodes: [...lst,chosenRequest]});
     this.refs[`autocomplete`].setState({searchText:''});
     this.refs[`autocomplete`].focus();
     if(this.state.chosenNodes.length>0){
        this.setState({color:blue500});
        this.setState({disable: false});
     }

  }

  render() {
    
    return <MuiThemeProvider muiTheme={getMuiTheme()}>
    <div>
      <AutoComplete hintText={"type here..."}
        ref={`autocomplete`}
        fullWidth={true}
        dataSource    = {this.state.dataSource}
        onNewRequest = {this.onNewRequest} />
      <MaterialUINodesList items={this.state.chosenNodes} />
      <div style={{margin:16}}>
  
      <ActionSend 
      onClick = {(e) => this.props.switchTab(this.state.chosenNodes)}
      disabled={this.state.disable} 
      color={this.state.color}/>

      </div> 
      </div>
      </MuiThemeProvider>
  }
}

export default MaterialUIAutocomplete;