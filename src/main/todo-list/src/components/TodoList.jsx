import { NavLink } from "react-router-dom";
import { motion } from "framer-motion";
import { useContext, useEffect, useState } from "react";

import styled from "styled-components";

import Input from "./Input"
import Spinner from "./Spinner";
import api from "../functions/api";
import { ListsContext } from "../context/listsContext";

const StyledDiv = styled.div`
  ${props=>props.isEditing&&"display: flex;"}
  // margin-top: 5px;
  height: 35px;
  justify-content: space-between;
  background-color: ${props=>props.isEditing?"rgba(100, 0, 0, 0.1)":"rgba(0, 0, 0, 0.1)"};
`

const TodoList = ({ className, listId, text, isEditing}) => {
  const {lists, setLists} = useContext(ListsContext);

  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [ETitle, setETitle] = useState(text);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const linkStyle = {
    display: "inline-block",
    width: "90%",
    height: "30px",
    fontSize: "1.1rem",
    padding: "5px 0px 5px 30px",
    overflow: "hidden",
    margin: "0"
  }

  const editStyle = {
    paddingLeft: "40px",
    width: "150px",
    height: "30px",
    fontSize: "1.1rem",
    margin: "0"
  }

  const deleteStyle = {
    color: "rgb(0, 0, 0)",
    marginRight: "10px",
    marginTop: "3px",
    cursor:"pointer",
  }

  const listNameStyle = {
    display: "inline-block",
    width: "90%",
    padding: "5px 0px 5px 30px",
    fontSize: "1.1rem",
    overflow: "hidden",
    borderBottom: "0px solid #d6d6d6",
    cursor:"pointer",
    height: "30px",
  }

  const checkStyle = {
    color: "rgb(0, 0, 0)",
    marginLeft: "10px",
    marginTop: "3px",
    cursor:"pointer",
  }

  const closeStyle = {
    color: "rgb(0, 0, 0)",
    marginRight: "10px",
    marginTop: "3px",
    cursor:"pointer",
  }

  const deleteTodoList = async() => {
    console.log(`Try to delete listId:${listId}`)
    setDeleteLoading(true)
    const res = await api.delete(`/api/v1/list/${listId}`);

    if(res.data.resultType === "S") {
      setLists(pre=>[...pre.filter(todoList=>todoList.listId !== listId)])
    } else if(res.data.resultType === "F") {
      switch(res.data.errorCode){
        case "AE_001":
          alert("잘못된 접근입니다.");
          break;
        default:
          alert("오?류")
      }
      setDeleteLoading(false)
    }
  }

  const changeTodoListTitle = async() => {
    console.log(`Try to change title ${text} to ${ETitle} listId:${listId}`)
    const res = await api.put(`/api/v1/list/${listId}`, {changeListName:ETitle}); //변경될 이름
    
    if(res.data.resultType === "S") {
      setIsEditingTitle(false);
      const newLists = []
      for(var list of lists){
        if (list.listId === listId) {
          list.listName = ETitle
        }
        newLists.push(list)
      }
      setLists(newLists);
    } else if(res.data.resultType === "F") {
      switch(res.data.errorCode) {
        case "AE_001":
          alert("잘못된 접근입니다.");
          break;
        case "LIE_001":
          alert("이미 존재하는 이름입니다.");
          break;
        case "LIE_002":
          alert("잘못된 이름입니다.");
          break;
        default:
          alert("오?류");
          break;
      }
    }
  }

  const onClickTitle = () => {
    setIsEditingTitle(pre=>!pre)
    setETitle(text)
  }
  const onChangeTitle = (e) => setETitle(e.target.value)

  useEffect(()=>{
    if(!isEditing)
      setIsEditingTitle(false)
  }, [isEditing])

  return (
    <StyledDiv data-listid={listId} isEditing={isEditing}>
      {!isEditing?
        <NavLink
          style={linkStyle}
          className={className}
          to={`/todo/${listId}`}
        >• {text}</NavLink>:
        !isEditingTitle?
          <motion.span 
            style={listNameStyle}
            whileHover={{
              scale:1.1
            }}
            onClick={onClickTitle}
          >
            • {text}
          </motion.span>:
          <Input 
            value={ETitle}
            onChange={onChangeTitle}
            style={editStyle}
          />
      }
      {isEditing && !isEditingTitle && (
        <span> 
          {deleteLoading?
            <Spinner />:
            <motion.span 
              className="material-symbols-outlined" 
              style={deleteStyle} 
              onClick={deleteTodoList}
              whileHover={{
                color:"rgb(250, 0, 0)", 
                scale: 1.3
              }}
            >
              delete
            </motion.span>
          }
        </span>
        
      )}

      {isEditingTitle && isEditing && (
        <motion.span 
          className="material-symbols-outlined" 
          style={checkStyle} 
          // onClick={deleteTodoList}
          whileHover={{
            color:"rgb(0, 150, 0)", 
            scale: 1.3
          }}
          onClick={changeTodoListTitle}
        >
          check
        </motion.span>
      )}

      {isEditingTitle && isEditing && (
        <motion.span 
          className="material-symbols-outlined" 
          style={closeStyle} 
          // onClick={deleteTodoList}
          whileHover={{
            color:"rgb(200, 50, 50)", 
            scale: 1.3
          }}
          onClick={onClickTitle}
        >
          Close
        </motion.span>
      )}
    </StyledDiv>
  );
}

export default TodoList;