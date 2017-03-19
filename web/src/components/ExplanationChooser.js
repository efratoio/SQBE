import React from 'react';
import {List, ListItem} from 'material-ui/List';
import SingleProvenanceCard from './ProvenanceChooser';
import EditorModeEdit from 'material-ui/svg-icons/editor/mode-edit';
import ReactDOM from 'react-dom';
import JSONP              from 'jsonp';
import $ from 'jquery';
import {red500, yellow800, blue800,blue500, green500} from 'material-ui/styles/colors';
import ActionDoneAll from 'material-ui/svg-icons/action/done-all';
import FloatingActionButton from 'material-ui/FloatingActionButton';
import ContentAdd from 'material-ui/svg-icons/content/add';
import FontIcon from 'material-ui/FontIcon';
import conf from "./constants"
const ajaxCallURL = `http://localhost:8080/Provenance?nodeList=`;

const NodesList = React.createClass({
    propTypes: {
        
        nodes: React.PropTypes.array,
        handleChoice: React.PropTypes.func,
        handleDone: React.PropTypes.func,
      },
 
   getDefaultProps: function() {
    return {
      nodes:[1,2]
    };
  },
  render: function() {
    return <div><List>
        {
      this.props.nodes.map((item,idx)=>(
         <ListItem 
         key={idx} 
         innerDivStyle={{paddingLeft: 100, padding: 10}}
         leftIcon={<FontIcon
          className="muidocs-icon-action-add"
          color={blue800}/>}
          
        primaryText={item.text}
       onClick={()=>{this.props.handleChoice(item.id, idx)}} />))
       }
    </List>
    <div>
    <ActionDoneAll onClick={()=>{this.props.handleDone()}} color={green500} />
    </div>
    </div>;
  }
});

const ExplanationChooser = React.createClass({
    propTypes: {
        
        nodes: React.PropTypes.array,
          handleDone: React.PropTypes.func,
      },
    getInitialState () {
    return {
      provMode: false,
      currentNode: null, 
      dataSource: [[[{"label":"1",id:1},{"label":"1",id:2}],
      [{"source":1,"target":2,"label":"3"}],{"num":1,"name":"1"}]]
    }     
  },
  componentWillReceiveProps (nextProps) {
    if(!conf.debug){
    const xhr = new XMLHttpRequest();
    URL = ajaxCallURL+nextProps.nodes.map((x)=>x.value.toString()).join(",");
    xhr.open('GET', URL);
    xhr.addEventListener('load', () => {
      const results = JSON.parse(xhr.responseText); 

      this.setState({dataSource: results,
       });
    });
    xhr.send();}
    else{
      this.setState({dataSource: [[[{label:"a",id:1},{label:"b",id:2}
        ,{label:"c",id:3},{label:"d",id:4},{label:"e",id:5}],
      [{source:1,target:2,label:"know",id:1},{source:2,target:3,label:"learned",id:2},
      {source:3,target:4,label:"related",id:3},{source:5,target:4,label:"uses",id:4},
      {source:5,target:2,label:"teaches",id:5}],{num:2,name:"b"}]],
       });
    }
    return true; 
  

  },
  chooseNode(id, idx){
    var data = this.state.dataSource[idx];
      this.setState({
        provMode: true,
        currentNode: id,
        data: data,
      });
  },
   getDefaultProps: function() {
    return {
      nodes:[]
    };
  },
  newProv(){
      this.setState({provMode: false});
  },
  render: function() {
    console.log("render explan chooos");
    if(this.state.provMode){

    return <SingleProvenanceCard sentData={this.newProv} data={this.state.data}/>;
    }else{
      return <NodesList nodes={this.props.nodes} 
      handleChoice={this.chooseNode} 
      handleDone={this.props.handleDone}/>;
    }
  }
  });
  export default ExplanationChooser;
