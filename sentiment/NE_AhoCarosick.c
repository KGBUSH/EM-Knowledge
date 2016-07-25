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
#define PATTERN_LEN_MAX          35 
#define PATTERN_COUNT_MAX        1200000
#define FILE_LEN_MAX             1000
#define NE_LEN_MAX               5

// For socket defines
#define QLEN                     32
#define BUF_LEN                  1024
#define PORT                     "16412"

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
   unsigned char c;
   //struct node *next[ASCII_MAX];
   struct node *next;
   struct node *sibling;
   struct node *failure;
};

char g_szInput[PATTERN_COUNT_MAX][PATTERN_LEN_MAX];
char g_szOutput[FILE_LEN_MAX];
int g_nCounter[PATTERN_COUNT_MAX];

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
   char szTemp[PATTERN_LEN_MAX],*pCh;
   unsigned char c;
   int i,j,nLen,fFound;
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
         fFound = 0;
         if (ptr->next != NULL)
         {
            ptr = ptr->next;
            while(ptr->c != c && ptr->sibling)
               ptr = ptr->sibling;
            if (ptr->c == c)
               fFound = 1;
         }
         else
            fFound = -1; //代表连 ptr->next 都是空的
         // fFound = 0 代表有 ptr->next，但是往后没找到 c，所以需要 new 一个 node 然后串在 sibling 的后面
         // fFound = 1 代表有找到，ptr 指在找到的 node 上
         // fFound = -1 代表连 ptr->next 都是空的

         ////////////////////////
         // 3.2. else => create a new node and link to ptr->next[c], ptr move down
         ////////////////////////
         if (fFound != 1) 
         {
            if ((ptr2 = (struct node *)malloc(sizeof(struct node))) == NULL)
               return ERR_MALLOC;
            memset(ptr2,0,sizeof(struct node));
            strncpy(ptr2->str,szTemp,PATTERN_LEN_MAX-1);
            ptr2->str[j+1] = '\0';
            ptr2->next = NULL;
            ptr2->sibling = NULL;
            ptr2->c = c;
            if (fFound == -1)
            {
               ptr->next = ptr2;
               ptr = ptr2;
            }
            else if (fFound == 0)
            {
               ptr->sibling = ptr2;
               ptr = ptr2;
            }
         }
      }

      ////////////////////////
      // 4. Set ptr as leaf node
      ////////////////////////
      ptr->isLeaf = 1;
      ptr->nPattern = i;

   }
   return SUCCESS;
} // end of BuildTree()

int BuildFailureLink(struct node *ptr, struct node *pRoot)
{
   char szTemp[PATTERN_LEN_MAX],szTemp2[PATTERN_LEN_MAX];
   unsigned char c;
   struct node *ptr2=NULL;
   int i;

   ////////////////////////
   // 1. Travese(DFS) the tree, start from root, (ptr=root)
   ////////////////////////
   if (ptr == NULL)
      return ERR_GENERAL;

   if (ptr->next != NULL)
   {
      ptr2 = ptr->next;
      BuildFailureLink(ptr2,pRoot);
      while(ptr2->sibling)
      {
         ptr2 = ptr2->sibling;
         BuildFailureLink(ptr2,pRoot);
      }
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
         strncpy(szTemp2,&szTemp[1],PATTERN_LEN_MAX-1);
         szTemp2[PATTERN_LEN_MAX-1] = '\0';
		   strcpy(szTemp,szTemp2);
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
            if (ptr2->next == NULL)
               break;
            ptr2 = ptr2->next;
            if (ptr2->c == c)
               continue;
            while(ptr2->sibling && ptr2->c != c)
            {
               ptr2 = ptr2->sibling;
            }
            if (ptr2->c == c)
               continue;
            else
               break;
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

int Travesal(char *szText, struct node *pRoot)
{
   struct node *ptr=NULL,*ptr2=NULL;
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
      //if (ptr->next[c] == NULL)
      ptr2 = ptr->next;
      while(ptr2)
      {
         if (ptr2->c == c)
            break;
         ptr2 = ptr2->sibling;
      }
      if (ptr2 == NULL)
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
         //ptr = ptr->next[c];
         ptr = ptr2;

         ////////////////////////
         // 4.1. if ptr->isLeaf => print
         ////////////////////////
         if (ptr->isLeaf)
         {
            if (g_szOutput[0] == '\0')
            {
               snprintf(g_szOutput,FILE_LEN_MAX-1,"%s=",ptr->str);
            }
            else
            {
               strcat(g_szOutput,"&");
               strcat(g_szOutput,ptr->str);
            }
            g_nCounter[ptr->nPattern] ++; // For logging matched pattern
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
   struct node *ptr2=NULL,*ptr3=NULL;

   if (ptr == NULL)
      return;

   ptr2 = ptr->next;
   while(ptr2)
   {
      ptr3 = ptr2->sibling;
      FreeMemory(ptr2);
      ptr2 = ptr3;
   }
   free(ptr);
} // end of FreeMemory()

int main(int argc, char* argv[])
{
   // For Aho-Corasick declare
   int nRet=SUCCESS,i=0,nLen=0;
   struct node *pRoot=NULL;
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
   memset(g_nCounter,0,sizeof(int)*PATTERN_COUNT_MAX);

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
      if ((nRet=Travesal(buf,pRoot)) != SUCCESS)
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

