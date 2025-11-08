/**
 * Menu items cho Admin Dashboard
 */
export const adminMenuItems = [
  {
    section: "Quản lý",
    items: [
      {
        label: "Phê duyệt hãng",
        icon: (
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
        ),
        onClick: () => {
          // TODO: Navigate to approval page
          console.log("Navigate to approval page");
        },
      },
      {
        label: "Quản lý sân bay",
        icon: (
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
            <circle cx="12" cy="10" r="3" />
          </svg>
        ),
        onClick: () => {
          // TODO: Navigate to airport management
          console.log("Navigate to airport management");
        },
      },
      {
        label: "Quản lý tuyến bay",
        icon: (
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path d="M21 16V14C21 12.9 20.1 12 19 12H5C3.9 12 3 12.9 3 14V16C3 17.1 3.9 18 5 18H19C20.1 18 21 17.1 21 16Z" />
            <path d="M7 10L12 15L17 10" />
          </svg>
        ),
        onClick: () => {
          // TODO: Navigate to route management
          console.log("Navigate to route management");
        },
      },
    ],
  },
  {
    section: "Báo cáo",
    items: [
      {
        label: "Báo cáo tổng hợp",
        icon: (
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <line x1="18" y1="20" x2="18" y2="10" />
            <line x1="12" y1="20" x2="12" y2="4" />
            <line x1="6" y1="20" x2="6" y2="14" />
          </svg>
        ),
        onClick: () => {
          // TODO: Navigate to reports
          console.log("Navigate to reports");
        },
      },
    ],
  },
  {
    section: "Hệ thống",
    items: [
      {
        label: "Cấu hình hệ thống",
        icon: (
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="12" cy="12" r="3" />
            <path d="M12 1v6m0 6v6M5.64 5.64l4.24 4.24m4.24 4.24l4.24 4.24M1 12h6m6 0h6M5.64 18.36l4.24-4.24m4.24-4.24l4.24-4.24" />
          </svg>
        ),
        onClick: () => {
          // TODO: Navigate to system settings
          console.log("Navigate to system settings");
        },
      },
    ],
  },
];
