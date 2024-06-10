import { createContext, useState } from "react";

export const ListsContext = createContext();

export const ListsProvider = ({ children }) => {
  const [lists, setLists] = useState([]);

  return (
    <ListsContext.Provider value={ {lists, setLists} }>
      {children}
    </ListsContext.Provider>
  )
}