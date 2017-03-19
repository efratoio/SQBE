import React from 'react';
import {Card, CardActions, CardHeader, CardMedia, CardTitle, CardText} from 'material-ui/Card';
import ReactDOM from 'react-dom';
import JSONP              from 'jsonp';
import $ from 'jquery';
import {red500, yellow800, blue800,blue500, green500} from 'material-ui/styles/colors';
import ActionDone from 'material-ui/svg-icons/action/done';
import ContentClear from 'material-ui/svg-icons/content/clear';
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';
import conf from "./constants";
import PresentGraph from "./PresentGraph";
import Dialog from 'material-ui/Dialog';
const ajaxCallURL = `http://localhost:8080/Diff`;


const DiffChooser = React.createClass({
    propTypes: {
        render: React.PropTypes.bool,
        nodes: React.PropTypes.array,
          handleDone: React.PropTypes.func,
      },
    getInitialState: function () {
    return {
    	open: false,
    	demi : false,
    	render: true,
      started:false,
      dataSource: [[{label:"a",id:1},{label:"b",id:2}
        ,{label:"c",id:3},{label:"d",id:4},{label:"e",id:5}],
      [{source:1,target:2,label:"know",id:1},{source:2,target:3,label:"learned",id:2},
      {source:3,target:4,label:"related",id:3},{source:5,target:4,label:"uses",id:4},
      {source:5,target:2,label:"teaches",id:5}],{num:2,name:"deafult"}]
    }     
  },
  onStart(){
    console.log("On Start");
    if(this.checkForNextQuestion()){
      this.getNextGraphFromServer();
      this.setState({started:true});
    }else{
      this.props.handleDone();
    }

  },
  handleAction: function(value){
  	
  	this.sendAnswerToServer(value);
  	if(this.checkForNextQuestion()){
  		this.getNextGraphFromServer()
  	}else{
  		this.props.handleDone();
  		return false;
  	}
  },
    sendRenderToServer: function(value){
  	console.log("CALL SERVER REQUEST RENDER");
  	if(!conf.debug){
  		var data= {request:"render"};
  		$.post(ajaxCallURL,JSON.stringify(data));
  	}
  },
  sendAnswerToServer: function(value){
  	
    console.log("CALL SERVER REQUEST ANSWER");
    if(!conf.debug){
      var data= {request:"answer",answer:value}
      $.post(ajaxCallURL,JSON.stringify(data));
    }
  },
 
    checkForNextQuestion: function(){
      console.log("checkForNextQuestion should be false",conf.debug);
  	if(!conf.debug){
    const xhr = new XMLHttpRequest();
    URL = ajaxCallURL+"?next";
    xhr.open('GET', URL);
    xhr.addEventListener('load', () => {
    	console.log("checkForNextQuestion",xhr.responseText);
      const results = JSON.parse(xhr.responseText); 
      return results.has;
    });
    xhr.send();}
    else{
      return true;
    }
  },
  getNextGraphFromServer: function(){
    console.log("CALL SERVER get question");
    if(!conf.debug){
    const xhr = new XMLHttpRequest();
    URL = ajaxCallURL+"?question";
    xhr.open('GET', URL);
    xhr.addEventListener('load', () => {
      console.log("Diff",xhr.responseText);
      const results = JSON.parse(xhr.responseText); 
      this.setState({dataSource: results,
       });
    });
    xhr.send();}
    else{
      this.setState({dataSource: [[{label:"a",id:1},{label:"b",id:2}
        ,{label:"c",id:3},{label:"d",id:4},{label:"e",id:5}],
      [{source:1,target:2,label:"know",id:1},{source:2,target:3,label:"learned",id:2},
      {source:3,target:4,label:"related",id:3},{source:5,target:4,label:"uses",id:4},
      {source:5,target:2,label:"teaches",id:5}],{num:2,name:"b"}]
       });
    }
  },


  componentWillReceiveProps(nextProps) {
  	if(nextProps.render && this.state.render){
  	this.sendRenderToServer();
  	this.setState({render:false})
  }
  	// if(this.checkForNextQuestion()){
  	// 	this.getNextGraphFromServer()
   //  	return true; 
  	// }else{
  	// 	this.props.handleDone();
  	// 	return false;
  	// }
  

  },

   getDefaultProps: function() {
    return {
      nodes:[]
    };
  },
  handleOpen: function (){
    this.setState({open: true});
  },

  handleClose : function(){
    this.setState({open: false});
  },
  newProv: function(){
      this.setState({provMode: false});
  },
  render: function() {
    if(!this.state.started){
      return <div>
      <div>
            You will now be presented with some entities that may or may not<br/>
            be part of the desired result set, and with the part of the graph that <br/>
            represents the explanation of their presence.
            </div>
            <RaisedButton 
            label="Start" 
            primary={true} 
            style={{margin:30}} 
            onClick={this.onStart} />
            </div>;
    }else{
  	  const actions = [
            <FlatButton
              label="Submit"
              primary={true}
              keyboardFocused={true}
              onTouchTap={this.handleClose}
            />,
          ];
    return   <Card style={conf.styles.Card}>
    <CardHeader
      title={this.state.dataSource[2].name}
    />
   
      <CardMedia >
      
        <div >
        <PresentGraph nodes={this.state.dataSource[0]} 
        links={this.state.dataSource[1]} 
        example = {this.state.dataSource[2].num}
        demi={this.state.demi}/>
        </div>

        </CardMedia> 

   
    <CardText>
      Is this node part of result set?
    </CardText>
    <CardActions>

    <RaisedButton style={{margin:6}} onClick={this.handleOpen}>
    Instructions
    </RaisedButton>
      <Dialog
          actions={actions}
          modal={false}
          open={this.state.open}
          onRequestClose={this.handleClose}
        >
          The nodes represent entities, while the edges represents relationships.
          Click the nodes that explains the example you chose.
          Then click on the edges that represents the right relationshops.
      </Dialog>

    <RaisedButton style={{margin:6}} onClick = {(e)=>this.handleAction(true)} 
     icon={<ActionDone
      color={green500}/> } >
    </RaisedButton>
  <RaisedButton style={{margin:6}} onClick = { (e) => this.handleAction(false)} 
     icon={<ContentClear color={red500}/>} />
      
    </CardActions>
  </Card>;
    }
  
  }});
  export default DiffChooser;
