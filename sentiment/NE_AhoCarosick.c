// Read POSITIVE and NEGATIVE files to do sentiment judge
// 
// #001  20160412    Created by Phantom

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <memory.h>
#include <stdarg.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#define INPUT_DICT_FILE          "entity.txt"
#define INPUT_TEXT_FILE          "text.txt"

#define ASCII_MAX                256
#define PATTERN_LEN_MAX          256 
#define PATTERN_COUNT_MAX        60000
#define FILE_LEN_MAX             1000
#define NE_LEN_MAX               5

// For socket defines
#define QLEN                     32
#define BUF_LEN                  1024
#define PORT                     "16413"

#define SUCCESS                  0
#define ERR_BASE                 0
#define ERR_GENERAL              ERR_BASE-1
#define ERR_OPEN_PATTERN         ERR_BASE-2
#define ERR_OPEN_TEXT            ERR_BASE-3
#define ERR_MALLOC               ERR_BASE-4

unsigned short portbase = 0;

struct node
{
   char str[PATTERN_LEN_MAX];
   int isLeaf;
   int nPattern;
   char szNE[NE_LEN_MAX];
   struct node *next[ASCII_MAX];
   struct node *failure;
};

char g_szInput[PATTERN_COUNT_MAX][PATTERN_LEN_MAX];
char g_szOutput[FILE_LEN_MAX];

int passivesock(const char *service, const char *transport, int qlen)
{
   struct servent *pse;
   struct protoent *ppe;
   struct sockaddr_in sin;
   int	s,type;

   ///////////////////////
   // Prepare sockaddr_in
   ///////////////////////
   memset(&sin, 0, sizeof(sin));
   sin.sin_family = AF_INET;
   sin.sin_addr.s_addr = INADDR_ANY;

   ///////////////////////
   // Map service name to port number
   ///////////////////////
   //if (pse = getservbyname(service, transport) )
   //   sin.sin_port = htons(ntohs((unsigned short)pse->s_port) + portbase);
   //else if ((sin.sin_port=htons(atoi(service))) == 0)
   if ((sin.sin_port=htons(atoi(service))) == 0)
   {
      printf("can't get \"%s\" service entry\n",service);
      return ERR_GENERAL;
   }

   ///////////////////////
   // Map protocol name to protocol number
   ///////////////////////
   //if ((ppe=getprotobyname(transport)) == 0)
   //{
   //   printf("can't get \"%s\" protocol entry\n", transport);
   //   return ERR_GENERAL;
   //}

   ///////////////////////
   // Use protocol to choose a socket type
   ///////////////////////
   if (strcmp(transport, "udp") == 0)
      type = SOCK_DGRAM;
   else
      type = SOCK_STREAM;

   ///////////////////////
   // Allocate a socket
   ///////////////////////
   //s = socket(PF_INET, type, ppe->p_proto);
   s = socket(PF_INET, type, 0);

   ///////////////////////
   // Bind the socket
   ///////////////////////
   if (s < 0)
   {
      return ERR_GENERAL;
   }
   if (bind(s, (struct sockaddr *)&sin, sizeof(sin)) < 0)
   {
      return ERR_GENERAL;
   }
   if (type == SOCK_STREAM && listen(s, qlen) < 0)
   {
      return ERR_GENERAL;
   }
   return s;
} // end of passivesock()

int passiveTCP(const char *service, int qlen)
{
   return passivesock(service, "tcp", qlen);
} // end of passiveTCP()

int BuildTree(char szInput[][PATTERN_LEN_MAX], int nNum, struct node *pRoot)
{
   char szTemp[PATTERN_LEN_MAX],*pCh,szNE[NE_LEN_MAX];
   unsigned char c;
   int i,j,nLen;
   struct node *ptr=NULL,*ptr2=NULL;

   ////////////////////////
   // 1. Read a string from the input pattern file
   ////////////////////////
   for (i=0;i<nNum;i++)
   {
      strncpy(szTemp,szInput[i],PATTERN_LEN_MAX-1);
      szTemp[PATTERN_LEN_MAX-1] = '\0';
      // find space or \r\n, set to '\0'
      nLen = strlen(szTemp);
      while(szTemp[nLen-1] == ' ' || szTemp[nLen-1] == '\r' || szTemp[nLen-1] == '\n')
      {
         szTemp[nLen-1] = '\0';
         nLen = strlen(szTemp);
      }
      pCh = strchr(szTemp,'\t');
      szNE[0] = '\0';
      if (pCh != NULL)
      {
         strncpy(szNE,pCh+1,NE_LEN_MAX-1);
         szNE[NE_LEN_MAX-1] = '\0';
         szTemp[pCh-szTemp] = '\0';
      }

      ////////////////////////
      // 2. Start from root (ptr = root)
      ////////////////////////
      if (pRoot == NULL)
         return ERR_GENERAL;
      ptr = pRoot;

      ////////////////////////
      // 3. For each character c, check ptr->next[c]
      ////////////////////////
      for (j=0;j<PATTERN_LEN_MAX;j++)
      {
         if (szTemp[j] == '\0')
            break;
         
         ////////////////////////
         // 3.1. if exist => move down
         ////////////////////////
         c = (unsigned char)szTemp[j];
         if (ptr->next[c] != NULL)
            ptr = ptr->next[c];

         ////////////////////////
         // 3.2. else => create a new node and link to ptr->next[c], ptr move down
         ////////////////////////
         else
         {
            if ((ptr2 = (struct node *)malloc(sizeof(struct node))) == NULL)
               return ERR_MALLOC;
            memset(ptr2,0,sizeof(struct node));
            strncpy(ptr2->str,szTemp,PATTERN_LEN_MAX-1);
            ptr2->str[j+1] = '\0';
            ptr->next[c] = ptr2;
            ptr = ptr2;
         }
      }

      ////////////////////////
      // 4. Set ptr as leaf node
      ////////////////////////
      ptr->isLeaf = 1;
      strncpy(ptr->szNE,szNE,NE_LEN_MAX-1);
      ptr->szNE[NE_LEN_MAX-1] = '\0';
      ptr->nPattern = i;

   }
   return SUCCESS;
} // end of BuildTree()

int BuildFailureLink(struct node *ptr, struct node *pRoot)
{
   char szTemp[PATTERN_LEN_MAX];
   unsigned char c;
   struct node *ptr2=NULL;
   int i;

   ////////////////////////
   // 1. Travese(DFS) the tree, start from root, (ptr=root)
   ////////////////////////
   if (ptr == NULL)
      return ERR_GENERAL;

   for (i=0;i<ASCII_MAX;i++)
   {
      if (ptr->next[i] != NULL)
         BuildFailureLink(ptr->next[i],pRoot);
   }

   ////////////////////////
   // 2. if (ptr->str is not NULL)
   ////////////////////////
   if (ptr->str[0] != '\0')
   {
      ////////////////////////
      // 2.1. copy ptr->str to szTemp
      ////////////////////////
      strncpy(szTemp,ptr->str,PATTERN_LEN_MAX-1);
      szTemp[PATTERN_LEN_MAX-1] = '\0';

      ////////////////////////
      // 2.2. cut the first char in szTemp
      ////////////////////////
      while(strlen(szTemp) > 0)
      {
         strncpy(szTemp,&szTemp[1],PATTERN_LEN_MAX-1);
         szTemp[PATTERN_LEN_MAX-1] = '\0';
         if (szTemp[0] == '\0')
            break;

         ////////////////////////
         // 2.3. ptr2=root, try to find szTemp 
         ////////////////////////
         ptr2 = pRoot;
         for (i=0;i<PATTERN_LEN_MAX;i++)
         {
            if (szTemp[i] == '\0')
               break;
            c = (unsigned char)szTemp[i];
            if (ptr2 == NULL)
               break;
            if (ptr2->next[c] == NULL)
               break;
            ptr2 = ptr2->next[c];
         }

         ////////////////////////
         // 2.4. if found => ptr->failure = ptr2 + break;
         ////////////////////////
         if (szTemp[i] == '\0')
         {
            ptr->failure = ptr2;
            break;
         }
      }

      ////////////////////////
      // 2.5. if szTemp='\0', ptr->failure=root
      ////////////////////////
      if (szTemp[0] == '\0')
         ptr->failure = pRoot;

   } // end of if (ptr->str[0] != '\0')
   return SUCCESS;
} // end of BuildFailureLink()

int Travesal(char *szText, struct node *pRoot, int nCounter[])
{
   struct node *ptr=NULL;
   unsigned char c;
   int i=0;

   ////////////////////////
   // 1. read input text file, ptr=root
   ////////////////////////
   if (szText == NULL)
      return ERR_OPEN_TEXT;
   if (pRoot == NULL)
      return ERR_GENERAL;

   ptr = pRoot;

   ////////////////////////
   // 2. get first char c, while(1)
   ////////////////////////
   c = (unsigned char)szText[i++];
   while(c != 0)
   {
      ////////////////////////
      // 3. if (ptr->next[c] == NULL)
      ////////////////////////
      if (ptr->next[c] == NULL)
      {
         ////////////////////////
         // 3.1. if ptr==root => next char c
         ////////////////////////
         if (ptr == pRoot)
            c = (unsigned char)szText[i++];

         ////////////////////////
         // 3.2. else => ptr=ptr->failure
         ////////////////////////
         else
            ptr = ptr->failure;
      }
      ////////////////////////
      // 4. else => ptr=ptr->next[c]
      ////////////////////////
      else
      {
         ptr = ptr->next[c];

         ////////////////////////
         // 4.1. if ptr->isLeaf => print
         ////////////////////////
         if (ptr->isLeaf)
         {
            if (g_szOutput[0] == '\0')
            {
               snprintf(g_szOutput,FILE_LEN_MAX-1,"%s=%s",ptr->str,ptr->szNE);
            }
            else
            {
               strcat(g_szOutput,"&");
               strcat(g_szOutput,ptr->str);
               strcat(g_szOutput,"=");
               strcat(g_szOutput,ptr->szNE);
            }
            nCounter[ptr->nPattern] ++; // For logging matched pattern
         }

         ////////////////////////
         // 4.2. next char c
         ////////////////////////
         c = (unsigned char)szText[i++];
      }
   } // end of while(!feof(fptr))
   return SUCCESS;
} // end of Travesal()

void FreeMemory(struct node *ptr)
{
   int i;

   if (ptr == NULL)
      return;
   for (i=0;i<ASCII_MAX;i++)
      if (ptr->next[i] != NULL)
         FreeMemory(ptr->next[i]);
   free(ptr);
} // end of FreeMemory()

int main(int argc, char* argv[])
{
   // For Aho-Corasick declare
   int nRet=SUCCESS,i=0,nLen=0;
   struct node *pRoot=NULL;
   int nCounter[PATTERN_COUNT_MAX];
   char s[PATTERN_LEN_MAX];
   char szText[FILE_LEN_MAX];
   FILE *fp=NULL;
   
   // For Socket declare
   int nRetCode = 0;
   int msock, ssock;
   char *service = PORT;
   int alen,n;
   struct sockaddr_in fsin;
   char buf[BUF_LEN];

   // init, create g_root
   if ((pRoot = (struct node *)malloc(sizeof(struct node))) == NULL)
   {
      printf("Malloc failed\n");
      return ERR_MALLOC;
   }
   memset(pRoot,0,sizeof(struct node));
   memset(nCounter,0,sizeof(int)*PATTERN_COUNT_MAX);

   //////////////////////////
   // Build Tree
   ////////////////////////// 
   if ((fp = fopen(INPUT_DICT_FILE,"r")) != NULL)
   {
      i = 0;
      while (fgets(s, PATTERN_LEN_MAX, fp) != NULL)
      {
         strncpy(g_szInput[i],s,PATTERN_LEN_MAX-1);
         nLen = strlen(g_szInput[i]);
         while (g_szInput[i][nLen-1] == '\r' || g_szInput[i][nLen-1] == '\n')
         {
            g_szInput[i][nLen-1] = '\0';
            nLen = strlen(g_szInput[i]);
         }
         i++;
      }
      if ((nRet=BuildTree(g_szInput,i,pRoot)) != SUCCESS)
         goto errexit;
      fclose(fp);
      fp = NULL;
   }

   //////////////////////////
   // Build Failure Link
   //////////////////////////
   if ((nRet=BuildFailureLink(pRoot,pRoot)) != SUCCESS)
      goto errexit;

   if ((fp = fopen(INPUT_TEXT_FILE,"r")) != NULL)
   {
      fgets(szText, FILE_LEN_MAX, fp);
      fclose(fp);
      fp = NULL;
   }

   ////////////////////////
   // Get Master socket
   ////////////////////////
   msock = passiveTCP(service, QLEN);
   //printf("\nSocket %d created successfully",msock);
   while (1) 
   {
      alen = sizeof(fsin);
      ////////////////////////
      // Block here and wait for request
      // Will return a Slave socket
      ////////////////////////
      ssock = accept(msock, (struct sockaddr *)&fsin, &alen);
      if (ssock < 0)
      {
         continue;
      }
      printf("\nSocket %d accepted",ssock);
      ////////////////////////
      // Read a string
      ////////////////////////
      memset(buf,0,BUF_LEN);
      n = recv(ssock, buf, BUF_LEN, 0);
      buf[BUF_LEN-1] = '\0';
      printf("\nString \"%s\" received in socket %d",buf,ssock);

      ////////////////////////
      // Reverse the string and send back */
      ////////////////////////
      g_szOutput[0] = '\0';
      if ((nRet=Travesal(buf,pRoot,nCounter)) != SUCCESS)
         goto errexit;
      printf("Result of [%s] is [%s]\n",buf,g_szOutput);
      n = strlen(g_szOutput);
      (void)send(ssock, g_szOutput, n, 0); // only send back the first byte
      (void)close(ssock);
   } // end of while(1)

	close(msock);

errexit:
   // Free memory
   FreeMemory(pRoot);
   return nRet;
}

