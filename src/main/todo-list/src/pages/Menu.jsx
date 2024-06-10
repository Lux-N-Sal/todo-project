import { useContext, useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { AnimatePresence, motion, useAnimationControls } from "framer-motion";
import styled from "styled-components";

import styles from "../styles/Menu.module.css";

import TodoList from "../components/TodoList";
import Spinner from "../components/Spinner";
import { logout } from "../functions/auth";
import api from "../functions/api";
import { ListsContext } from "../context/listsContext";

const ToDoListInput = styled.input`
  margin-left: 35px;
  width: 180px;
`

const activeStyle = {
  backgroundColor : "#606060",
  color: "white"
}

const Menu = () => {
  const { lists, setLists } = useContext(ListsContext);

  const [expended, setExpended] = useState(false);
  const [inputExpended, setInputExpended] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [toDoListName, setToDoListName] = useState("");
  // const [toDoLists, setToDoLists] = useState([]);
  const [menuExpended, setMenuExpended] = useState(false);
  const [addListLoading, setAddListLoading] = useState(false);

  const controls = useAnimationControls();

  const linkVariants = {
    hidden:{opacity:0},
    show:{opacity: menuExpended ? 1:0, transition:{delay: menuExpended ? 0.2:0}},
  }

  const onChangeToDoListName = (e) => setToDoListName(e.target.value);

  const createToDoList = async() => {
    setAddListLoading(true);
    const res = await api.post("/api/v1/list/create", { listName: toDoListName });

    if (res.data.resultType === "S"){
      setLists(pre=>[...pre, res.data.body])
      expendInput();
    } else if(res.data.resultType === "F") {
      switch(res.data.errorCode){
        case "LIE_001":
          alert("이미 존재하는 이름입니다.")
          break;
        case "LIE_002":
          alert("잘못된 이름입니다.");
          break;
        default:
          alert("오?류");
          break;
      }
    }
    setAddListLoading(false);
  }

  const getToDoListData = async() => {
    if ((sessionStorage.getItem("accessToken") != null) && lists.length === 0) {
      const res = await api.get("/api/v1/list/lists");
      if (res.data.resultType === "S"){
        setLists(res.data.body.toDoListDto)
      } else if (res.data.resultType === "F") {
        console.log(res.data.errCode)
      }
    }
  };

  useEffect(()=>{
    // getToDoListData();
  }, [expended])

  const expendToDoList  = () => setExpended(pre=>!pre)
  const expendInput = () => setInputExpended(pre=>!pre)
  const edit = () => setIsEditing(pre=>!pre)
  const expandMenu = () => setMenuExpended(true)
  const closeMenu = () => {setMenuExpended(isEditing)}

  const expendTodoLists = async() => {
    if(expended){
      await controls.set({display:"block"})
      controls.start({
        height: "auto"
      })
    } else {
      await controls.start({
        height: "0px",
      })
      controls.set({display:"none"})
    }
  }

  useEffect(()=>{
    expendTodoLists();
  }, [controls, expended])

  // const deleteTodoList = (listId) => {
  //   setLists(pre=>[...pre.filter(todoList=>todoList.listId !== listId)])
  // }

  const TodoLists = () => {
    return (
      lists.map((toDoList, idx) => 
        <TodoList 
          key={idx}
          className={styles.todoList} 
          listId={toDoList.listId}
          text={toDoList.listName}
          isEditing={isEditing}
        />)
    )
  }

  return (
    <motion.div 
      className={styles.menu} 
      // style={menuStyle}
      animate={{
        width: menuExpended ? "280px": "100px", 
        flexGrow: menuExpended ? 2:1,
      }}
      transition = {{delay: menuExpended ? 0:0.2}}
      // whileHover={{flexGrow:2}}
      onMouseEnter={expandMenu}
      onMouseLeave={closeMenu}
    >
      <motion.div
        variants={linkVariants}
        initial="hidden"
        animate="show"
      >
        <NavLink className={styles.menuLink} style={({isActive}) => (isActive ? activeStyle:{})}  to={"/"}>메인</NavLink>
      </motion.div>
      {sessionStorage.getItem("accessToken")!=null && (
        <motion.div 
          className={styles.todoList}
          variants={linkVariants}
          initial="hidden"
          animate="show"
        >
          <div style={{marginBottom:"15px"}}>
            Todo 리스트
            <motion.span 
              className={`material-symbols-outlined ${styles.expend}`} 
              onClick={expendToDoList} 
              animate={{
                rotate: expended ? 90:0,
                transformOrigin: "15px 15px"
              }}
              whileHover={{
                cursor:"pointer",
                scale:1.2,
              }}  
            >
              expand_more
            </motion.span>
            <AnimatePresence>
            {expended && (
              <motion.span 
                className={`material-symbols-outlined ${styles.plus}`}
                onClick={expendInput}
                animate={{
                  rotate: inputExpended ? 45:0,
                  transformOrigin: "10px 10px",
                  opacity:1,
                }}
                whileHover={{
                  cursor:"pointer",
                  scale:1.2,
                }}
                initial={{opacity:0}}
                exit={{opacity:0}}
              >
                add
              </motion.span>
            )}
            </AnimatePresence>
            <AnimatePresence>
            {expended && (
              <motion.span 
                className={`material-symbols-outlined ${styles.edit}`}
                onClick={edit}
                animate={{
                  rotate: isEditing ? -45:0,
                  transformOrigin: "10px 10px",
                  opacity:1
                }}
                whileHover={{
                  cursor:"pointer",
                  scale:1.2,
                }}
                initial={{opacity:0}}
                exit={{opacity:0}}
              >
                edit
              </motion.span>
            )}
            </AnimatePresence>
          </div>

          {lists&&(
            <motion.div
              style={{overflow:"hidden"}}
              initial={{display:"none", height:"0px"}}
              animate={controls}
            >
              <TodoLists />
              {inputExpended&&(
                <div style={{
                    backgroundColor: "rgba(0, 0, 0, 0.1)", 
                    paddingBottom: "5px",
                  }}>
                  <ToDoListInput 
                    onChange={onChangeToDoListName}
                    value={toDoListName}
                  />
                  <span style={{textAlign:"center"}}>
                    {addListLoading?
                      <Spinner styles={{fontSize:"24px", x:"80%"}}/>:
                      <motion.span
                        className={`material-symbols-outlined`}
                        onClick={createToDoList}
                        initial={{
                          color:"rgb(0, 0, 0)",
                          pointer: "cursor",
                          paddingLeft: "15px",
                        }}
                        whileHover={{color:"rgb(100, 250, 100)", scale: 1.3}}
                      >
                        done
                      </motion.span>
                    }
                  </span>
                  
                </div>)
                }
            </motion.div>
            )
          }
        </motion.div>
      )}
      {sessionStorage.getItem("accessToken")!=null && (
        <motion.div
          variants={linkVariants}
          initial="hidden"
          animate="show"
        >
          <NavLink className={styles.menuLink} style={({isActive}) => (isActive ? activeStyle:{})} to={"/mypage"}>내정보</NavLink>
        </motion.div>)}

      {sessionStorage.getItem("accessToken")!=null && (
        <motion.div
          variants={linkVariants}
          initial="hidden"
          animate="show"
        >
          <NavLink 
            className={styles.menuLink} 
            onClick={()=>{
              logout();
            }}
          >로그아웃</NavLink>
        </motion.div>)}
        
      {sessionStorage.getItem("accessToken")==null && (
        <motion.div
          variants={linkVariants}
          initial="hidden"
          animate="show"
        >
          <NavLink className={styles.menuLink} style={({isActive}) => (isActive ? activeStyle:{})} to={"/user/login"}>로그인</NavLink>
        </motion.div>)}

      {sessionStorage.getItem("accessToken")==null && (
        <motion.div
          variants={linkVariants}
          initial="hidden"
          animate="show"
        >
          <NavLink className={styles.menuLink} style={({isActive}) => (isActive ? activeStyle:{})} to={"/user/join"}>회원가입</NavLink>
        </motion.div>)}
      
      {/* {cookies.accessToken==null && (
        <Button 
          text={"로그인 상태 만들기 (메뉴상에서만)"}
          onClick={tempLogin}
        />
      )} */}
    </motion.div>
  );
}

export default Menu;