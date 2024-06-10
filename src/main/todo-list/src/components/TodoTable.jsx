import { motion } from "framer-motion";
import { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";

import styles from "../styles/TodoTable.module.css"

import Todo from "./Todo"
import api from "../functions/api";
import { ListsContext } from "../context/listsContext";

const TodoTable = ({listId, reload, setIsLoading}) => {
  const {lists} = useContext(ListsContext);

  const [todos, setTodos] = useState([]);
  const [listName, setListName] = useState("");

  const [toggleDone, setToggleDone] = useState(false);

  const calcLeftDate = () => {
    todos.map((todo, idx)=> {
      let leftDate = new Date(todo.deadline) - new Date();
      todos[idx].leftDate = leftDate<0?-1:leftDate
    })
  }

  const getListName = () => {
    for (var list of lists) {
      if (listId === list.listId) {
        setListName(list.listName)
      }
    }
  }

  const deleteTodo = (todoId) => {
    setIsLoading(true)
    setTodos(pre=>[...pre.filter(todo=>todo.toDoId !== todoId)])
    setIsLoading(false)
  }

  const getTodos = async() => {
    setIsLoading(true)
    const res = await api.get(`/api/v1/list/${listId}/todos`);
    if (res.data.resultType === "S"){
      setTodos([...res.data.body])
    } else if (res.data.resultType === "F") {
      console.log(res.data.errCode)
    }
    setIsLoading(false)
  }

  useEffect(()=>{
    calcLeftDate()
  }, [todos])

  useEffect(()=>{
    setIsLoading(false)
    getListName()
    getTodos()
  }, [listId, reload])

  return (
    
    <div className={styles.todoTable}>
      <div className={styles.funcBar}>
        <div className={styles.a}>
            <Link to={`/todo/${listId}`}>
              {listName}
            </Link>
        </div>
        <div className={styles.func}>
          <motion.div
            className={styles["func-btn"]}
            whileHover={{
              scale:1.1,
            }}
            onClick={()=>{setToggleDone(pre=>!pre)}}
          >
            {toggleDone?"완료":"미완료"}
          </motion.div>
        </div>
      </div>
      
      <div className={styles.hr} />

      <div 
        className={styles.table}
        style = {{ 
          overflowY : todos.length > 4 ? "scroll" : "none" 
        }}
      >
        <div>
          {todos.map((todo, idx)=> {
            return(
              <Todo
                key={idx}
                listId={listId}
                todo={todo}
                getTodos={getTodos}
                deleteTodo={deleteTodo}
                toggleDone={toggleDone}
              />)
          })}
        </ div>
      </div>
    </div>
  );
}

export default TodoTable;