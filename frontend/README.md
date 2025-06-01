This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Development Testing Hub

The project includes a centralized testing hub accessible at [http://localhost:3000/dev-testing](http://localhost:3000/dev-testing) that provides:

- Links to all component testing pages
- Easy access to UI component previews and tests
- Navigation to specific testing utilities

Available test pages include:
- Captcha verification testing
- Success alert component testing
- Image gallery testing

This hub makes it easier to test individual components in isolation and verify translations are working correctly.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.

## API Error Handling

The application implements a standardized approach to API error handling with:

- Consistent error objects
- User-friendly error messages with translations
- Proper error classification

For more details, see [API Error Handling Documentation](docs/api_error_handling.md).

### Recent Changes

- **Removed backend connectivity checking**: We've eliminated the automatic server health checking system to reduce unnecessary backend requests. API requests now fail gracefully with appropriate error messages when the backend is unavailable.
