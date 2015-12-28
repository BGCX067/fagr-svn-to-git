
clear;
tic
%f2uDirNamesStruct=dir([pwd '\f2u*']); %change as necessary

% f2uDirNamesStruct=dir([pwd '\U_nFBnMnD*']);
% f2uDirNamesStruct=dir([pwd '\f2u.xst.fb*']);
% f2uDirNamesStruct=dir([pwd '\f2u.xst.co*']);
maximumNumOfGenerations=30002;
fprintf('maximum Num Of Generations = %d \n',maximumNumOfGenerations);
if(maximumNumOfGenerations==0)
    maximumNumOfGenerations=input('enter maximumNumOfGenerations: ');
end
% fprintf('1 \n');
f2uDirNamesStruct=dir([pwd '/f2u*']);
% fprintf('2 \n');
paramMat=zeros(size(f2uDirNamesStruct,1),6); % 6< => uxp c s mu mp mc
% fprintf('3 \n');
% fprintf('%6.2f',length(f2uDirNamesStruct));
for i =1:length(f2uDirNamesStruct)
    %     fprintf('4 \n');
    workDirName=f2uDirNamesStruct(i).name;
    %     fprintf('5 \n');
    cd(workDirName);
    %     fprintf('6 \n');
    %     paramMat(i,1)=sscanf(workDirName((findstr(workDirName,'.uxp.')+5):findstr(workDirName,'.c.')-1),'%f');
    %     paramMat(i,2)=sscanf(workDirName((findstr(workDirName,'.c.')+3):findstr(workDirName,'.s.')-1),'%f');
    %     paramMat(i,3)=sscanf(workDirName((findstr(workDirName,'.s.')+3):findstr(workDirName,'.mu.')-1),'%f');
    %     paramMat(i,4)=sscanf(workDirName((findstr(workDirName,'.mu.')+4):findstr(workDirName,'.mp.')-1),'%f');
    %     paramMat(i,5)=sscanf(workDirName((findstr(workDirName,'.mp.')+4):findstr(workDirName,'.mc.')-1),'%f');
    %     paramMat(i,6)=sscanf(workDirName((findstr(workDirName,'.mc.')+4):findstr(workDirName,'.mod.')-1),'%f');
    %     fprintf('7 \n');
    csvFilesNamesStruct=dir('f2u*.csv');
    %     fprintf('8 \n');
    averagesMat=csvread(csvFilesNamesStruct(1).name,1,0);
    %     fprintf('%d %d \n',size(averagesMat));
    catHelpMat1=zeros(maximumNumOfGenerations-size(averagesMat,1),size(averagesMat,2));
    if (averagesMat(end,2)>=0.95 ||averagesMat(end,9) >=0.95)
        catHelpMat1=repmat(averagesMat(end,:),size(catHelpMat1,1),1);
    end
    averagesMat=cat(1,averagesMat,catHelpMat1);
    %     fprintf('9 \n');
    for j =2:length(csvFilesNamesStruct)
        
        %         fprintf('%6.2f \n', length(csvFilesNamesStruct));
        %         if i==4
        %             i
        %         end
        %         fprintf('10 \n');
        fprintf('%6.2f %6.2f \n',i,j);
        currCsvFile=csvread(csvFilesNamesStruct(j).name,1,0);
        catHelpMat2=zeros(maximumNumOfGenerations-size(currCsvFile,1),size(currCsvFile,2));
        if (currCsvFile(end,2)>=0.95 ||currCsvFile(end,9) >=0.95)
            catHelpMat2=repmat(currCsvFile(end,:),size(catHelpMat2,1),1);
        end
        currCsvFile=cat(1,currCsvFile,catHelpMat2);
        %         fprintf('11 \n');
        %         fprintf('%d %d ; %d %d \n',size(averagesMat),size(currCsvFile));
        averagesMat=cat(3,averagesMat,currCsvFile);
        %         fprintf('12 \n');
    end
    
    
    stdMat=std(averagesMat,0,3);
    averageTimeSeriesMat=mean(averagesMat,3);
    save averageData averageTimeSeriesMat stdMat workDirName
    clear averageTimeSeriesMat stdMat averagesMat
    
    cd ..
end

% save paramsList paramMat
toc
