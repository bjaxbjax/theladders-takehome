import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '../../src/style.css'
import AdminPage from './AdminPage.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AdminPage />
  </StrictMode>,
)
